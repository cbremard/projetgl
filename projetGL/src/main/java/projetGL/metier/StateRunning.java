package projetGL.metier;

public class StateRunning implements State {

	public float compute(Method method) throws Exception {
		throw new Exception("Can't compute a running method");
	}

}
