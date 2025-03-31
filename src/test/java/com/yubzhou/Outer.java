package com.yubzhou;

public class Outer {
	int x = 10;
	int y = 20;
	void method() {
		int x = 30;
		Runnable r = () -> {
			System.out.println(x);         // 30（局部变量，需是final或effectively final）
			System.out.println(this.x);    // 10（外部类的x）
			System.out.println(y);		   // 20（外部类的y）
			System.out.println(this.y);    // 20（外部类的y）
			System.out.println(this);	   // 输出外部类Outer的实例地址
		};
		r.run();
	}

	public static void main(String[] args) {
		new Outer().method();
	}
}

class Parent {
	void print() {
		System.out.println(this); // 输出子类实例地址
	}
}

class Child extends Parent {
}

class Main {
	public static void main(String[] args) {
		Parent parent = new Child();
		parent.print();
	}
}