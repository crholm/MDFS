package mdfs.namenode.parser;

import mdfs.utils.parser.Parser;

/**
 * Creatats a parsers implementing the interface Parser depending on what is needed to pars a Session
 * @author Rasmus Holm
 *
 */
public class ParserFactory {
	/**
	 * Returns a parser that are able parse MDFS communication accordingly
	 * @param stage can be "Request", "Response" or "Info"
	 * @param type can be "File", "Meta-Data" or "Info",
	 * @param mode can be "Write",  "Read",  "Remove" or "Info"
	 * @return a Parser that handles specified parameters or null if unavailable 
	 */
	public Parser getParser(String stage, String type, String mode){
		
		//Returns a parser for Stage = Request
		if(stage.equals("Request")){
			//Returns a parser for Type = Meta-data
			if(type.equals("Meta-data")){
				return new ParserRequestMetaData(mode);
			}
		//Returns a parser for Stage = Info
		}else if(stage.equals("Info")){
			//Returns a parser for Type = File 
			if(type.equals("File")){
				return new ParserInfoFile(mode);
			}
		}
		
		
		return null;
	}
}
