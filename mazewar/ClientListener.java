/*
Copyright (C) 2004 Geoffrey Alan Washburn
  
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.
  
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
  
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
USA.
 */

/**
 * An interface for objects wishing to subscribe to notifications actions
 * preformed by a {@link Client}.
 * 
 * @author Geoffrey Washburn &lt;<a
 *         href="mailto:geoffw@cis.upenn.edu">geoffw@cis.upenn.edu</a>&gt;
 * @version $Id: ClientListener.java 336 2004-01-23 19:14:42Z geoffw $
 */

public interface ClientListener {

	/**
	 * Invoked when a {@link Client} performs an action.
	 * 
	 * @param client
	 *            The {@link Client} acting.
	 * @param clientevent
	 *            A {@link ClientEvent} specifying the action performed.
	 */
	void clientUpdate(Client client, ClientEvent clientevent);

}
