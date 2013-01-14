package model;

public class Member {
    private String id;
    private String name;
    
    public Member(String id, String name) {
	super();
	this.id = id;
	this.name = name;
    }    
    
    /**
     * @return the id
     */
    public String getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
}
