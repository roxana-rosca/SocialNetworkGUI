package ro.ubbcluj.map.socialnetworkgui.repository.paging;

import java.util.stream.Stream;

public interface Page<E> {
    Pageable getPageable();

    Pageable nextPageable();

    Stream<E> getContent();

    void setPageable(Pageable pageable);

    void setContent(Stream<E> stream);
}
