package demo;

import tw.framework.michaelcore.ioc.Core;
import tw.framework.michaelcore.ioc.annotation.MichaelCore;

@MichaelCore
public class Application {

	public static void main(String[] args) throws Exception {
		Core.start();
	}

}