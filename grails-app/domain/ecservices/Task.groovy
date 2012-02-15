package ecservices

class Task {

    static constraints = {
    }
	
	public String owner
	public String createDate
	public String description
	public String complete
	public String resolution
	public String dueDate
	
	public String getOwner() {
		return owner
	}
	public void setOwner(String owner) {
		this.owner = owner
	}
	public String getCreateDate() {
		return createDate
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate
	}
	public String getDescription() {
		return description
	}
	public void setDescription(String description) {
		this.description = description
	}
	public String getComplete() {
		return complete
	}
	public String setComplete(String complete) {
		this.complete = complete
	}
	public String getResolution() {
		return resolution
	}
	public String setResolution(String resolution) {
		this.resolution = resolution
	}
	public String getDueDate() {
		return dueDate
	}
	public String setDueDate(String dueDate) {
		this.dueDate = dueDate
	}
	
	
}
