/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package freenet.node.fcp;

import java.net.MalformedURLException;

import freenet.client.FailureCodeTracker;
import freenet.client.InserterException;
import freenet.keys.FreenetURI;
import freenet.node.Node;
import freenet.support.Fields;
import freenet.support.SimpleFieldSet;

public class PutFailedMessage extends FCPMessage {

	final int code;
	final String codeDescription;
	final String extraDescription;
	final String shortCodeDescription;
	final FailureCodeTracker tracker;
	final FreenetURI expectedURI;
	final String identifier;
	final boolean global;
	final boolean isFatal;
	
	public PutFailedMessage(InserterException e, String identifier, boolean global) {
		this.code = e.getMode();
		this.codeDescription = InserterException.getMessage(code);
		this.shortCodeDescription = InserterException.getShortMessage(code);
		this.extraDescription = e.extra;
		this.tracker = e.errorCodes;
		this.expectedURI = e.uri;
		this.identifier = identifier;
		this.global = global;
		this.isFatal = InserterException.isFatal(code);
	}

	/**
	 * Construct from a fieldset. Used in serialization of persistent requests.
	 * Will need to be made more tolerant of syntax errors if is used in an FCP
	 * client library. FIXME.
	 * @param useVerboseFields If true, read in verbose fields (CodeDescription
	 * etc), if false, reconstruct them from the error code.
	 * @throws MalformedURLException 
	 */
	public PutFailedMessage(SimpleFieldSet fs, boolean useVerboseFields) throws MalformedURLException {
		identifier = fs.get("Identifier");
		if(identifier == null) throw new NullPointerException();
		global = fs.getBoolean("Global", false);
		code = Integer.parseInt(fs.get("Code"));
		
		if(useVerboseFields) {
			codeDescription = fs.get("CodeDescription");
			isFatal = Fields.stringToBool(fs.get("Fatal"), false);
			shortCodeDescription = fs.get("ShortCodeDescription");
		} else {
			codeDescription = InserterException.getMessage(code);
			isFatal = InserterException.isFatal(code);
			shortCodeDescription = InserterException.getShortMessage(code);
		}
		
		extraDescription = fs.get("ExtraDescription");
		String euri = fs.get("ExpectedURI");
		if(euri != null && euri.length() > 0)
			expectedURI = new FreenetURI(euri);
		else
			expectedURI = null;
		SimpleFieldSet trackerSubset = fs.subset("Errors");
		if(trackerSubset != null) {
			tracker = new FailureCodeTracker(true, trackerSubset);
		} else {
			tracker = null;
		}
	}

	public SimpleFieldSet getFieldSet() {
		return getFieldSet(true);
	}
	
	public SimpleFieldSet getFieldSet(boolean verbose) {
		SimpleFieldSet fs = new SimpleFieldSet(true);
		fs.putSingle("Identifier", identifier);
		if(global) fs.putSingle("Global", "true");
		fs.put("Code", code);
		if(verbose)
			fs.putSingle("CodeDescription", codeDescription);
		if(extraDescription != null)
			fs.putSingle("ExtraDescription", extraDescription);
		if(tracker != null) {
			fs.tput("Errors", tracker.toFieldSet(verbose));
		}
		if(verbose)
			fs.put("Fatal", isFatal);
		if(verbose)
			fs.putSingle("ShortCodeDescription", shortCodeDescription);
		if(expectedURI != null)
			fs.putSingle("ExpectedURI", expectedURI.toString());
		return fs;
	}

	public String getName() {
		return "PutFailed";
	}

	public void run(FCPConnectionHandler handler, Node node)
			throws MessageInvalidException {
		throw new MessageInvalidException(ProtocolErrorMessage.INVALID_MESSAGE, "PutFailed goes from server to client not the other way around", identifier, global);
	}

}
