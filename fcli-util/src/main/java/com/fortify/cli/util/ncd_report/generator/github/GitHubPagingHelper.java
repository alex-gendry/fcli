package com.fortify.cli.util.ncd_report.generator.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fortify.cli.common.rest.paging.INextPageUrlProducer;
import com.fortify.cli.common.rest.paging.LinkHeaderPagingHelper;

import kong.unirest.HttpRequest;
import kong.unirest.PagedList;

/**
 * This class provides utility methods for handling GitHub paging.
 * 
 * @author rsenden
 */
public class GitHubPagingHelper {
    private GitHubPagingHelper() {}
    
    public static final <R extends JsonNode> PagedList<R> pagedRequest(HttpRequest<?> request, Class<R> returnType) {
        return LinkHeaderPagingHelper.pagedRequest(request, nextPageUrlProducer(), returnType);
    }
    
    public static final INextPageUrlProducer nextPageUrlProducer() {
        return LinkHeaderPagingHelper.linkHeaderNextPageUrlProducer("Link", "next");
    }
}
