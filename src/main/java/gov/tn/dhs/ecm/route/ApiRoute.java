package gov.tn.dhs.ecm.route;

import com.fasterxml.jackson.core.JsonParseException;
import gov.tn.dhs.ecm.exception.ServiceErrorException;
import gov.tn.dhs.ecm.model.*;
import gov.tn.dhs.ecm.service.*;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
class ApiRoute extends RouteBuilder {

    public final DeleteDocumentService deleteDocumentService;

    @Value("${server.port}")
    private String serverPort;

    @Value("${runstatus}")
    private String runStatus;

    public ApiRoute(DeleteDocumentService deleteDocumentService) {
        this.deleteDocumentService = deleteDocumentService;
    }

    @Override
    public void configure() {

        onException(JsonParseException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(constant("{}"))
        ;

        onException(Exception.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(simple("${exception.message}"))
        ;

        onException(ServiceErrorException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, simple("${exception.code}"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(simple("${exception.message}"))
        ;

        restConfiguration()
                .enableCORS(true)
                .apiProperty("cors", "true") // cross-site
                .component("servlet")
                .port(serverPort)
                .bindingMode(RestBindingMode.json)
                .dataFormatProperty("prettyPrint", "true");

        rest()
                .post("/")
                .type(DocumentDeletionRequest.class)
                .outType(DocumentDeletionResult.class)
                .to("direct:deleteDocumentService")
        ;
        from("direct:deleteDocumentService")
                .bean(deleteDocumentService)
                .endRest()
        ;

        rest()
                .get("/")
                .to("direct:runningStatus")
        ;
        from("direct:runningStatus")
                .log("runStatus property value is " + runStatus)
                .process(exchange -> exchange.getIn().setBody(new SimpleMessage(runStatus), SimpleMessage.class))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .setHeader("Content-Type", constant("application/json"))
                .endRest()
        ;

    }

}
