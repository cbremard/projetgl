package projetGL.metier;



public class StateReady implements State {

	private Method _method;
	
	public StateReady(Method method) {
		super();
		_method = method;
	}

	
	public float compute(Method method) throws Exception{
		return method.compute();
	}


}