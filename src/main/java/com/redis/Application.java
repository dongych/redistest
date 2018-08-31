package com.redis;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan
public class Application {
	public static void main(String[] paramArrayOfString) {
		try {

			new SpringApplicationBuilder(new Object[0]).bannerMode(Banner.Mode.CONSOLE)
					.sources(new Class[] { Application.class }).run(paramArrayOfString);

		} catch (BeanCreationException localBeanCreationException) {
			System.err.println("请确认已正确配置该引擎，请检查是否正确配置(规则、数据装载等基本信息)");
			localBeanCreationException.printStackTrace();
		}
	}
}