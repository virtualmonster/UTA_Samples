import * as path from 'path';
import { ExtensionContext } from 'vscode';
import { LanguageClient, LanguageClientOptions, ServerOptions } from 'vscode-languageclient';

const languageID: string = 'hcl-onetest-script';
export function activate(context: ExtensionContext) {
	console.log('Extension "HCL OneTest Script Language Server Extension" active');
	
	//Java 11
	let { JAVA_HOME } = process.env;
	console.log(`Using java from JAVA_HOME: ${JAVA_HOME}`);
	if (JAVA_HOME) {
		let excecutable: string = path.join(JAVA_HOME, 'bin', 'java');
		const fatJarName: string = 'com.hcl.products.onetest.lang.script.ide-1.0.0-SNAPSHOT-ls.jar';
		let classPath : string = path.join(__dirname, '..', fatJarName);
		let args: string[] = ['-cp', classPath];
		let launcherClass: string = 'org.eclipse.xtext.ide.server.ServerLauncher';
		let serverOptions: ServerOptions = {
			command: excecutable,
			args: [...args, launcherClass],
			options: {}
		};
		let clientOptions: LanguageClientOptions = {
			documentSelector: [{ scheme: 'file', language: languageID }]
		};
		let client: LanguageClient = new LanguageClient(languageID, 'HCL OneTest Script Language Server', serverOptions, clientOptions);
		context.subscriptions.push(client.start());
	}
}
