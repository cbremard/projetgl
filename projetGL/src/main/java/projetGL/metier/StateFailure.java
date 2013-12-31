package projetGL.metier;

public class StateFailure implements State {

	public void compute(Method method) throws Exception {
		throw new Exception("Can't compute again a fail method");
	}

}
