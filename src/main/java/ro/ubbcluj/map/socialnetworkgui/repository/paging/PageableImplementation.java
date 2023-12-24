package ro.ubbcluj.map.socialnetworkgui.repository.paging;

public class PageableImplementation implements Pageable {

    private int pageNumber; // in ui: <1,2,3,...> + previous,next, how many on a page
    private int pageSize;

    public PageableImplementation(int pageNumber, int pageSize) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    @Override
    public int getPageNumber() {
        return this.pageNumber;
    }

    @Override
    public int getPageSize() {
        return this.pageSize;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
