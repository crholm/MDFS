package mdfs.namenode.parser;

import mdfs.utils.io.protocol.enums.Mode;
import mdfs.utils.io.protocol.enums.Stage;
import mdfs.utils.io.protocol.enums.Type;
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
	public Parser getParser(Stage stage, Type type, Mode mode){
		
		//Returns a parser for Stage = Request
		if(stage == Stage.REQUEST){
			//Returns a parser for Type = Meta-data
			if(type == Type.METADATA){
				return new ParserRequestMetaData(mode);
			}else if(type == Type.INFO){
                return new ParserRequestInfo(mode);
            }

		//Returns a parser for Stage = Info
		}else if(stage == Stage.INFO){
			//Returns a parser for Type = File 
			if(type == Type.FILE){
				return new ParserInfoFile(mode);
			}
		}
		
		
		return null;
	}
}
