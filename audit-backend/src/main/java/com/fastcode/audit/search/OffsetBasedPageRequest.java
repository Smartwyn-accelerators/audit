package com.fastcode.audit.search;

import java.io.Serializable;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class OffsetBasedPageRequest implements Pageable, Serializable {

    private static final long serialVersionUID = 1L;

    private final long offset;
    private final int pageSize;
    private final Sort sort;

    public OffsetBasedPageRequest(long offset, int pageSize) {
        this(offset, pageSize, Sort.unsorted());
    }

    public OffsetBasedPageRequest(long offset, int pageSize, Sort sort) {
        if (offset < 0) throw new IllegalArgumentException("Offset must be >= 0");
        if (pageSize < 1) throw new IllegalArgumentException("Page size must be >= 1");
        this.offset = offset;
        this.pageSize = pageSize;
        this.sort = (sort == null) ? Sort.unsorted() : sort;
    }

    @Override
    public int getPageNumber() {
        return (int) (offset / pageSize);
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new OffsetBasedPageRequest(offset + pageSize, pageSize, sort);
    }

    @Override
    public Pageable previousOrFirst() {
        long newOffset = offset - pageSize;
        return newOffset >= 0
                ? new OffsetBasedPageRequest(newOffset, pageSize, sort)
                : first();
    }

    @Override
    public Pageable first() {
        return new OffsetBasedPageRequest(0, pageSize, sort);
    }

    @Override
    public boolean hasPrevious() {
        return offset > 0;
    }

    /** New in Spring Data 3.x */
    @Override
    public Pageable withPage(int pageNumber) {
        if (pageNumber < 0) throw new IllegalArgumentException("Page index must be >= 0");
        long newOffset = (long) pageNumber * pageSize;
        return new OffsetBasedPageRequest(newOffset, pageSize, sort);
    }
}
