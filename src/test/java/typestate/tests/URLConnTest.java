package typestate.tests;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.junit.Test;

import ideal.Analysis;
import ideal.ResultReporter;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import test.IDEALTestingFramework;
import typestate.TypestateDomainValue;
import typestate.impl.urlconn.URLConnAnalysis;

public class URLConnTest extends IDEALTestingFramework {

	@Test
	public void test1() throws IOException {
	    HttpURLConnection httpURLConnection = new HttpURLConnection(null) {

	        @Override
	        public void connect() throws IOException {
	          // TODO Auto-generated method stub
	          System.out.println("");
	        }

	        @Override
	        public boolean usingProxy() {
	          // TODO Auto-generated method stub
	          return false;
	        }

	        @Override
	        public void disconnect() {
	          // TODO Auto-generated method stub

	        }
	      };
	      httpURLConnection.connect();
	      httpURLConnection.setDoOutput(true);
	      mustBeInErrorState(httpURLConnection);
	      httpURLConnection.setAllowUserInteraction(false);
	      mustBeInErrorState(httpURLConnection);
	}

	@Test
	public void test2() throws IOException {
	    HttpURLConnection httpURLConnection = new HttpURLConnection(null) {

	        @Override
	        public void connect() throws IOException {
	          // TODO Auto-generated method stub
	          System.out.println("");
	        }

	        @Override
	        public boolean usingProxy() {
	          // TODO Auto-generated method stub
	          return false;
	        }

	        @Override
	        public void disconnect() {
	          // TODO Auto-generated method stub

	        }
	      };
	      httpURLConnection.setDoOutput(true);
	      httpURLConnection.setAllowUserInteraction(false);

	      httpURLConnection.connect();
	      mustBeInAcceptingState(httpURLConnection);
	}

	@Override
	protected Analysis<TypestateDomainValue> createAnalysis(ResultReporter<TypestateDomainValue> reporter) {
		return new URLConnAnalysis(new InfoflowCFG(), reporter);
	}

}