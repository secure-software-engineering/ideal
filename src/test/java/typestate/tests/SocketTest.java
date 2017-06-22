package typestate.tests;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.junit.Test;

import ideal.debug.IDebugger;
import ideal.debug.NullDebugger;
import test.IDEALTestingFramework;
import test.slowmethod.SlowMethodDetector;
import typestate.ConcreteState;
import typestate.TypestateChangeFunction;
import typestate.TypestateDomainValue;
import typestate.impl.statemachines.SocketStateMachine;

public class SocketTest extends IDEALTestingFramework {

	@Test
	public void test1() throws IOException {
		Socket socket = new Socket();
		socket.connect(new SocketAddress() {
		});
		socket.sendUrgentData(2);
		mustBeInAcceptingState(socket);
	}

	@Test
	public void test2() throws IOException {
		Socket socket = new Socket();
		socket.sendUrgentData(2);
		mustBeInErrorState(socket);
	}

	@Test
	public void test3() throws IOException {
		Socket socket = new Socket();
		socket.sendUrgentData(2);
		socket.sendUrgentData(2);
		mustBeInErrorState(socket);
	}



	@Override
	protected TypestateChangeFunction<ConcreteState> createTypestateChangeFunction() {
		return new SocketStateMachine();
	}

	@Override
	protected IDebugger<TypestateDomainValue<ConcreteState>> getDebugger() {
		return new NullDebugger<>();
	}
}
