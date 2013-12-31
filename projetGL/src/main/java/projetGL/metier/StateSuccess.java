package projetGL.metier;

public class StateSuccess implements State {

	public void compute(Method method) throws Exception {
		throw new Exception("Can't compute again a successful method");
	}

}
