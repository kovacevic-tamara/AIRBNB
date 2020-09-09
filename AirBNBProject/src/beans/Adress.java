package beans;

public class Adress {

	public String country;
	public String street;
	public int numberOfStreet;
	public String city;
	public int postNumber;
	
	public Adress() {}
	
	public Adress(String country, String street, int numberOfStreet, String city, int postNumber) {
		super();
		this.country=country;
		this.street = street;
		this.numberOfStreet = numberOfStreet;
		this.city = city;
		this.postNumber = postNumber;
	}
	

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	public int getNumberOfStreet() {
		return numberOfStreet;
	}
	public void setNumberOfStreet(int numberOfStreet) {
		this.numberOfStreet = numberOfStreet;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public int getPostNumber() {
		return postNumber;
	}
	public void setPostNumber(int postNumber) {
		this.postNumber = postNumber;
	}
	
	
}
