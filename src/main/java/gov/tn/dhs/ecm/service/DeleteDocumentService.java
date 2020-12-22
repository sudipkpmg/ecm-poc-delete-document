package gov.tn.dhs.ecm.service;

import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFile;
import gov.tn.dhs.ecm.model.DocumentDeletionRequest;
import gov.tn.dhs.ecm.model.DocumentDeletionResult;
import gov.tn.dhs.ecm.util.ConnectionHelper;
import gov.tn.dhs.ecm.util.JsonUtil;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DeleteDocumentService extends BaseService {

    private static final Logger logger = LoggerFactory.getLogger(DeleteDocumentService.class);

    public DeleteDocumentService(ConnectionHelper connectionHelper) {
        super(connectionHelper);
    }

    public void process(Exchange exchange) {
        DocumentDeletionRequest documentDeletionRequest = exchange.getIn().getBody(DocumentDeletionRequest.class);
        logger.info("Delete document called with payload {}", JsonUtil.toJson(documentDeletionRequest));
        String documentId = documentDeletionRequest.getDocumentId();
        String appUserId = documentDeletionRequest.getAppUserId();


        try {
            BoxDeveloperEditionAPIConnection api = getBoxApiConnection();
//        api.asUser(appUserId);
            BoxFile file = new BoxFile(api, documentId);
            file.delete();
        } catch (BoxAPIException e) {
            if (e.getResponseCode() == 404) {
                setupError("404", "Document not found");
            }
            setupError("500", "Document deletion error");
        }

        DocumentDeletionResult documentDeletionResult = new DocumentDeletionResult();
        documentDeletionResult.setMessage("document with id " + documentId + " successfully deleted");
        logger.info("Delete document success response is {}", JsonUtil.toJson(documentDeletionResult));
        setupResponse(exchange, "200", documentDeletionResult, DocumentDeletionResult.class);
    }

}


