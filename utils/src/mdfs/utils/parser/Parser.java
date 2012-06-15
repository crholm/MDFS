package mdfs.utils.parser;


/**
 * A interface that defines what a parser should be able to do.
 * A parser parses the Session and creates the response. A Session is there for more of a container for the request and response.
 * @author Rasmus Holm
 *
 */
public interface Parser {
	/**
	 * Parses the session, build a response and edits it content of the session.
	 * @param session is the session that wraps content to be parsed
	 * @return true-if the parsing and creating of valid response was successful
	 */
	public boolean parse(Session session);
	

}
