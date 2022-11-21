package br.com.pucsp.systemsintegration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.language.xpath.XPathBuilder;
import org.json.JSONObject;

import br.com.pucsp.systemsintegration.utils.User;

public class MyRouteBuilder extends RouteBuilder {
    public void configure() {
    	ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        from("file:src/input?noop=true")
        	.split(xpath("//user"))
        	.parallelProcessing().streaming().executorService(threadPool)
        	.setHeader("cep", xpath("//cep/text()").convertToString())
        	.setHeader("cpf", xpath("//cpf/text()").convertToString())
        	.process((exc) -> {
        		User user = new User();
        		user.setCpf(String.valueOf(XPathBuilder.xpath("//cpf/text()").evaluate(exc, String.class)));
        		user.setFirstName(String.valueOf(XPathBuilder.xpath("//firstName/text()").evaluate(exc, String.class)));
        		user.setLastName(String.valueOf(XPathBuilder.xpath("//lastName/text()").evaluate(exc, String.class)));
        		user.setCep(String.valueOf(XPathBuilder.xpath("//cep/text()").evaluate(exc, String.class)));
        		
        		exc.getIn().setHeader("user", user);
        	})
        	
        	.setBody(simple("${null}"))
        	
        	.toD("https://viacep.com.br/ws/${header.cep}/json" +
        		    "?httpMethod=GET")
        	
        	.process((exc) -> {
        		User user = exc.getMessage().getHeader("user", User.class);
        		JSONObject body = new JSONObject(exc.getMessage().getBody(String.class));
        		
        		String city = body.get("localidade") + "";
        		String state = body.get("uf") + "";
        		
        		user.setCity(city);
        		user.setState(state);
        		
        		exc.getMessage().setBody(user);
        		exc.getMessage().removeHeader("user");
        	})
        	.marshal().json()
        	.to("file:src/output?filename=${header.cpf}");
    }
}