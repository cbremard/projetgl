package projetGL.metier;

public class StateRunning implements State {

	public void compute(Method method) throws Exception {
		throw new Exception("Can't compute a running method");
	}

}
