package projetGL.metier;


public class StateReady implements State {

	public float compute(Method method) {
		return method.compute();
	}

}