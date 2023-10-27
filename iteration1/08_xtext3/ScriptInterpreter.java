package com.hcl.products.onetest.lang.script.interpreter;

import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.interpreter.IEvaluationContext;
import org.eclipse.xtext.xbase.interpreter.impl.DefaultEvaluationContext;
import org.eclipse.xtext.xbase.interpreter.impl.EvaluationException;
import org.eclipse.xtext.xbase.interpreter.impl.InterpreterCanceledException;
import org.eclipse.xtext.xbase.interpreter.impl.XbaseInterpreter;

import com.hcl.products.onetest.lang.script.ScriptStandaloneSetup;
import com.hcl.products.onetest.lang.script.script.CallMacro;
import com.hcl.products.onetest.lang.script.script.Macro;
import com.hcl.products.onetest.lang.script.script.Model;
import com.hcl.products.onetest.lang.script.script.Pause;
import com.hcl.products.onetest.lang.script.script.StringToMacroMapEntry;
import com.hcl.products.onetest.lang.script.script.TimeUnits;

public class ScriptInterpreter extends XbaseInterpreter {
	private static final Map<TimeUnits, TimeUnit> TO_JVM_TIME_UNIT = Map.of(//
			TimeUnits.HOUR, TimeUnit.HOURS, //
			TimeUnits.MINUTE, TimeUnit.MINUTES, //
			TimeUnits.SECOND, TimeUnit.SECONDS, //
			TimeUnits.MILLI_SECOND, TimeUnit.MILLISECONDS);

	private static final QualifiedName LOGGER = QualifiedName.create("log");

	private static Model getContainingModel(XExpression expression) {
		return (Model) Stream.iterate(expression, EObject::eContainer).filter(e -> e instanceof Model).findFirst()
				.get();
	}

	private static Macro getMacro(EList<StringToMacroMapEntry> macros, String key) {
		return macros.stream().filter(i -> i.getKey().equals(key)).findFirst().map(i -> i.getValue()).get();
	}

	private static Model loadModel(URI uri, boolean loadOnDemand) {
		XtextResourceSet resSet = new XtextResourceSet();
		Resource resource = resSet.getResource(uri, loadOnDemand);

		if (!resource.getContents().isEmpty()) {
			if (resource.getContents().get(0) instanceof Model) {
				return (Model) resource.getContents().get(0);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static void log(IEvaluationContext ctx, String message) {
		((Consumer<String>) ctx.getValue(LOGGER)).accept(message);
	}

	private static Consumer<String> newLog(PrintStream ps, Object txId) {
		return (Consumer<String>) s -> ps.println(String.format("[%s] %s", txId, s));
	}

	public static void main(String[] args) throws InterruptedException {
		Instant init = Instant.now();
		ScriptStandaloneSetup.doSetup();

		Model model = loadModel(URI.createFileURI("C:\\hcl\\dsl-2021-03\\workspace\\scratch\\my.script"), true);

		ScriptInterpreter interpreter = new ScriptInterpreter();

		ExecutorService executor = Executors.newFixedThreadPool(3);// lets imagine the OS can only manage 3 threads...
		Instant begin = Instant.now();
		for (int txId = 1; txId <= 10; txId++) {// but we have 10 transactions to run...
			DefaultEvaluationContext context = new DefaultEvaluationContext();
			context.newValue(LOGGER, newLog(System.out, txId));

			executor.execute(() -> {
				interpreter.evaluate(model.getSteps(), context, CancelIndicator.NullImpl);
			});
		}

		executor.shutdown();
		while (!executor.awaitTermination(24L, TimeUnit.HOURS)) {
			System.out.println("Not yet. Still waiting for termination");
		}

		Instant end = Instant.now();

		// If we switch out newFixedThreadPool for a VirtualThread pool from Loom my
		// assumption is this completes in 3s, instead of 12s (4 batches of 3s).

		System.out.println("time to init: " + Duration.between(init, begin));
		System.out.println("time for batch: " + Duration.between(begin, end));
	}

	protected Object _doEvaluate(CallMacro expression, IEvaluationContext context, CancelIndicator indicator) {
		log(context, "call " + expression.getURL());

		String url = expression.getURL();
		if (url.startsWith("#")) {
			String id = url.substring("#".length());
			Macro macro = getMacro(getContainingModel(expression).getMacros(), id);

			// TODO is fork all we need here to signify a new frame to the interpreter?
			return doEvaluate(macro.getSteps(), context.fork(), indicator);
		}
		throw new RuntimeException("TODO");
	}

	protected Object _doEvaluate(Pause expression, IEvaluationContext context, CancelIndicator indicator) {
		log(context, "sleeping " + expression.getDuration() + expression.getUnit());

		try {
			Thread.sleep(TO_JVM_TIME_UNIT.get(expression.getUnit()).toMillis(expression.getDuration()));
		} catch (InterruptedException e) {
			throw new InterpreterCanceledException();
		}
		return null;
	}

	// TODO should we be overriding this method or fixing up #typeResolver so we can
	// override doEvaluate instead?
	@Override
	protected Object internalEvaluate(XExpression expression, IEvaluationContext context, CancelIndicator indicator)
			throws EvaluationException {
		if (indicator.isCanceled())
			throw new InterpreterCanceledException();

		if (expression instanceof CallMacro) {
			return _doEvaluate((CallMacro) expression, context, indicator);
		} else if (expression instanceof Pause) {
			return _doEvaluate((Pause) expression, context, indicator);
		}

		return doEvaluate(expression, context, indicator);
	}
}
