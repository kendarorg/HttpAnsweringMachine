package org.kendar.http.annotations.concrete;

import org.kendar.http.annotations.HamDoc;
import org.kendar.http.annotations.multi.*;

import java.lang.annotation.Annotation;

@SuppressWarnings("ClassExplicitlyAnnotation")
public class HamDocConcrete implements HamDoc {
    private final HamDoc doc;

    public HamDocConcrete(HamDoc doc) {

        this.doc = doc;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }

    @Override
    public String[] tags() {
        return doc.tags();
    }

    @Override
    public boolean todo() {
        return false;
    }

    @Override
    public String description() {
        return doc.description();
    }

    @Override
    public String produce() {
        return doc.produce();
    }

    @Override
    public QueryString[] query() {
        return doc.query();
    }

    @Override
    public PathParameter[] path() {
        return doc.path();
    }

    @Override
    public Header[] header() {
        return doc.header();
    }

    @Override
    public HamRequest[] requests() {
        return doc.requests();
    }

    @Override
    public HamResponse[] responses() {
        return doc.responses();
    }

    @Override
    public HamSecurity[] security() {
        return doc.security();
    }
}
