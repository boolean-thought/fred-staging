/* This code is part of Freenet. It is distributed under the GNU General
 * Public License, version 2 (or at your option any later version). See
 * http://www.gnu.org/ for further details of the GPL. */
package freenet.node.fcp;

import freenet.node.Node;
import freenet.node.PeerNode;
import freenet.support.SimpleFieldSet;

public class RemovePeer extends FCPMessage {

	static final String name = "RemovePeer";
	
	final SimpleFieldSet fs;
	
	public RemovePeer(SimpleFieldSet fs) {
		this.fs = fs;
	}

	public SimpleFieldSet getFieldSet() {
		return new SimpleFieldSet(true);
	}

	public String getName() {
		return name;
	}

	public void run(FCPConnectionHandler handler, Node node) throws MessageInvalidException {
		String nodeIdentifier = fs.get("NodeIdentifier");
		PeerNode pn = node.getPeerNode(nodeIdentifier);
		if(pn == null) {
			FCPMessage msg = new UnknownNodeIdentifierMessage(nodeIdentifier);
			handler.outputHandler.queue(msg);
			return;
		}
		String identity = pn.getIdentityString();
		node.removeDarknetConnection(pn);
		handler.outputHandler.queue(new PeerRemoved(identity, nodeIdentifier));
	}

}
