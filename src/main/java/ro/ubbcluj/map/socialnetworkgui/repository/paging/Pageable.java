package ro.ubbcluj.map.socialnetworkgui.repository.paging;

public interface Pageable {
    int getPageNumber();

    int getPageSize();

    void setPageNumber(int pageNo);

    void setPageSize(int pageSize);
}
