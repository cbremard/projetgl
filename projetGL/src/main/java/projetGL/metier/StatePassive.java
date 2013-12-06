package projetGL.metier;


public class StatePassive implements State{

	public float compute(Method method) throws Exception {
		throw new Exception("Can't compute a Passive method");
	}

}
