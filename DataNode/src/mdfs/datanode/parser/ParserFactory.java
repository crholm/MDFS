package mdfs.datanode.parser;


import mdfs.utils.io.protocol.enums.Mode;
import mdfs.utils.io.protocol.enums.Stage;
import mdfs.utils.io.protocol.enums.Type;
import mdfs.utils.parser.Parser;

/**
 * Creates a parser in regard to Stage, Type and Mode. I i used i together with a session
 * @author Rasmus Holm
 *
 */
public class ParserFactory {
	/**
	 * Creates a parser in regard to Stage, Type and Mode. To be used together with a session
	 * @param stage the stage of the header
	 * @param type	the type of the header
	 * @param mode the mode of the header
	 * @return a Parser fit for the needs.
	 */
	public Parser getParser(Stage stage, Type type, Mode mode){

		
		if(stage == Stage.REQUEST){
			if(type == Type.FILE){
				return new ParserRequestFile(mode);
			}
		}
		
		
		return null;
	}
}
