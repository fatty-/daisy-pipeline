package org.daisy.pipeline.job;

import java.net.URI;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import javax.xml.transform.Source;
import javax.xml.transform.Source;

import org.daisy.common.base.Provider;

import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcPipelineInfo;
import org.daisy.common.xproc.XProcPortInfo;

import org.daisy.pipeline.script.XProcOptionMetadata;
import org.daisy.pipeline.script.XProcScript;
import org.daisy.pipeline.script.XProcScript;

class Mock   {
	public static JobContext mockContext(JobId id){
		return new AbstractJobContext(id,null,null){
			@Override
			public void writeXProcResult() {
			}

			@Override
			public Set<URI> getFiles() {
				return null;
			}

			@Override
			public URI getZip() {
				return null;
			}

			@Override
			public URI toZip(URI... files) {
				return null;
			}

		};
	}

	public static class MockSource implements Source,Provider<Source>{
		String sId;

		/**
		 * Constructs a new instance.
		 *
		 * @param sId The sId for this instance.
		 */
		public MockSource(String sId) {
			this.sId = sId;
		}

		@Override
		public String getSystemId() {
			return sId;
		}

		@Override
		public void setSystemId(String systemId) {
			sId=systemId;
			
		}

		@Override
		public Source provide() {
			return this;
		}
	}

	public static Provider<Source> getSourceProvider(String systemId){
		return new MockSource(systemId);

	}
	//generates a script with the asked fetures
	//the port/option names follow the convection
	//Input: input-x
	//options:
	// output file: option-output-file-x
	// output dir: option-output-dir-x
	// input file: option-input-file-x
	// input dir: option-input-x
	// other types: option-x
	// 
	public static class ScriptGenerator{
		public static String INPUT="input";
		public static String OUTPUT="output";
		public static String VALUE="value";
		public static String OPTION="option";
		public static String FILE="file";
		public static String DIR="dir";

		public static class Builder{
			int inputs;
			int optionOutputsFile;
			int optionOutputsDir;
			int optionInputs;
			int optionOther;
			int optionOutputsNA;

			/**
			 * Sets the inputs for this instance.
			 *
			 * @param inputs The inputs.
			 */
			public Builder withInputs(int inputs) {
				this.inputs = inputs;
				return this;
			}

			/**
			 * Sets the optionOutputsFile for this instance.
			 *
			 * @param optionOutputsFile The optionOutputsFile.
			 */
			public Builder withOptionOutputsFile(int optionOutputsFile) {
				this.optionOutputsFile = optionOutputsFile;
				return this;
			}

			/**
			 * Sets the optionOutputsDir for this instance.
			 *
			 * @param optionOutputsDir The optionOutputsDir.
			 */
			public Builder withOptionOutputsDir(int optionOutputsDir) {
				this.optionOutputsDir = optionOutputsDir;
				return this;
			}

			/**
			 * Sets the optionInputsFile for this instance.
			 *
			 * @param optionInputsFile The optionInputsFile.
			 */
			public Builder withOptionInputs(int optionInputs) {
				this.optionInputs= optionInputs;
				return this;
			}

			/**
			 * Sets the optionOther for this instance.
			 *
			 * @param optionOther The optionOther.
			 */
			public Builder withOptionOther(int optionOther) {
				this.optionOther = optionOther;
				return this;
			}

			public ScriptGenerator build(){
				return new ScriptGenerator( inputs, optionOutputsFile, optionOutputsDir, optionInputs, optionOther,optionOutputsNA);
			}

			/**
			 * Sets the optionOutputsNA for this instance.
			 *
			 * @param optionOutputsNA The optionOutputsNA.
			 */
			public Builder withOptionOutputsNA(int optionOutputsNA) {
				this.optionOutputsNA = optionOutputsNA;
				return this;
			}

		}

		int inputs;
		int optionOutputsFile;
		int optionOutputsDir;
		int optionInputs;
		int optionOther;
		int optionOutputsNA;

		/**
		 * Constructs a new instance.
		 *
		 * @param inputs The inputs for this instance.
		 * @param optionOutputsFile The optionOutputsFile for this instance.
		 * @param optionOutputsDir The optionOutputsDir for this instance.
		 * @param optionInputsFile The optionInputsFile for this instance.
		 * @param optionInputsDir The optionInputsDir for this instance.
		 * @param optionOther The optionOther for this instance.
		 */
		public ScriptGenerator(int inputs, int optionOutputsFile,
				int optionOutputsDir, int optionInputs,
				 int optionOther,int optionsOutputNA) {
			this.inputs = inputs;
			this.optionOutputsFile = optionOutputsFile;
			this.optionOutputsDir = optionOutputsDir;
			this.optionInputs= optionInputs;
			this.optionOther = optionOther;
			this.optionOutputsNA= optionsOutputNA;
		}


		public XProcScript generate(){
			Set<XProcPortInfo> inputSet= new HashSet<XProcPortInfo>(); 
			Set<XProcOptionInfo> optionsSet= new HashSet<XProcOptionInfo>(); 
			HashMap<QName,XProcOptionMetadata> optionMetadatas= new HashMap<QName,XProcOptionMetadata>(); 
			//inputs
			for (int i=0;i<this.inputs;i++){
				inputSet.add(XProcPortInfo.newInputPort(getInputName(i),false, true));
			}

			//options inputs
			for (int i=0;i<this.optionInputs;i++){
				QName name=getOptionInputName(i);
				optionsSet.add( XProcOptionInfo.newOption(name, false, ""));
				optionMetadatas.put(name,new XProcOptionMetadata.Builder()
				.withType(MappingURITranslator.TranslatableOption.ANY_FILE_URI.toString()).build());
			}
			//options output file
			for (int i=0;i<this.optionOutputsFile;i++){
				QName name= getOptionOutputFileName(i);
				optionsSet.add( XProcOptionInfo.newOption(name, false, ""));
				optionMetadatas.put(name,new XProcOptionMetadata.Builder()
				.withType(MappingURITranslator.TranslatableOption.ANY_FILE_URI.toString()).withOutput("result").build());
			}

			//options output file
			for (int i=0;i<this.optionOutputsDir;i++){
				QName name= getOptionOutputDirName(i);
				optionsSet.add( XProcOptionInfo.newOption(name, false, ""));
				optionMetadatas.put(name,new XProcOptionMetadata.Builder()
				.withType(MappingURITranslator.TranslatableOption.ANY_DIR_URI.toString()).withOutput("result").build());
			}

			//options output file
			for (int i=0;i<this.optionOutputsNA;i++){
				QName name= getOptionOutputNAName(i);
				optionsSet.add( XProcOptionInfo.newOption(name, false, ""));
				optionMetadatas.put(name,new XProcOptionMetadata.Builder()
				.withType(MappingURITranslator.TranslatableOption.ANY_DIR_URI.toString()).withOutput("NA").build());
			}
			//regular options 
			for (int i=0;i<this.optionOther;i++){
				QName name= getRegularOptionName(i);
				optionsSet.add( XProcOptionInfo.newOption(name, false, ""));
				optionMetadatas.put(name,new XProcOptionMetadata.Builder()
				.build());
			}
			XProcPipelineInfo.Builder pipelineBuilder= new XProcPipelineInfo.Builder();	
			for (XProcOptionInfo oInf:optionsSet){
				pipelineBuilder.withOption(oInf);
			}
			for(XProcPortInfo port:inputSet){
				pipelineBuilder.withPort(port);
			}
			return new XProcScript(pipelineBuilder.build(), null, null, null, null, optionMetadatas,null);

		}

		public static String getInputName(int num){
				return (String.format("%s-%d",INPUT,num));
		}
		public static QName getOptionInputName(int num){
				return new QName(String.format("%s-%s-%d",OPTION,INPUT,num));
		}


		public static QName getOptionOutputFileName(int num){
				return new QName(String.format("%s-%s-%s-%d",OPTION,OUTPUT,FILE,num));
		}
		public static QName getOptionOutputDirName(int num){
				return new QName(String.format("%s-%s-%s-%d",OPTION,OUTPUT,DIR,num));
		}
		public static QName getRegularOptionName(int num){
				return new QName(String.format("%s-%d",OPTION,num));
		}

		public static QName getOptionOutputNAName(int num){
				return new QName(String.format("%s-na-%d",OPTION,num));
		}
	}


}
