package services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;


import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import beans.Apartment;
import beans.Comment;
import beans.Reservation;
import beans.User;
import beans.UserType;
import dao.ApartmentDAO;
import dao.ReservationDAO;
import dao.UserDAO;

@Path("/userService")
public class UserService {

	@Context 
	ServletContext sc;
	
	private static final String JWT_TOKEN_KEY = "yZJsnXojT3";
	private static final int Token_Time = 24;
	private static final int Refresh_Token = 300;
	
	public UserService() {
		// TODO Auto-generated constructor stub
	}
	
	@PostConstruct
	public void init() {
		if (sc.getAttribute("userDAO") == null) {
	    	String contextPath = sc.getRealPath("");
			sc.setAttribute("userDAO", new UserDAO(contextPath));
		}
	}
	
	@POST
	@Path("/blockUser")
	@Secured({UserType.Admin})
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public User block(User user) throws JsonSyntaxException, IOException {
		
		if(user.getBlocked())
			throw new BadRequestException();
		
		UserDAO userDao = new UserDAO();
		ApartmentDAO apartmentDao = new ApartmentDAO();
		ReservationDAO reservationDao = new ReservationDAO();
		if(user.getRole() == UserType.Admin)
			throw new BadRequestException();
		else if(user.getRole() == UserType.Guest) {
			//brisanje rezervacija
			for(Reservation res : reservationDao.GetAll()) {
				if(res.getGuest().getUsername().equals(user.getUsername()))
					//ovde ima i rejectReservation za apartman
					reservationDao.Delete(res);
			}
			
			//brisanje komentara
			for(Apartment a : apartmentDao.GetAll()) {
				ArrayList<Comment> comments = new ArrayList<Comment>();
				for(Comment c : a.getComments()) {
					if(c.getGuest().getUsername().equals(user.getUsername())) {
						c.setIsApproved(false);
					}
					comments.add(c);
				}
				a.setComments(comments);
				apartmentDao.Edit(a);
				for(Reservation res : reservationDao.GetAll()) {
					if(res.getApartment().getId() == a.getId()) {
						res.setApartment(a);
						reservationDao.Edit(res);
					}
				}
			}
	
		}else if(user.getRole() == UserType.Host) {
			for(Apartment apartment : apartmentDao.GetAll()) {
				if(apartment.getHost().getUsername().equals(user.getUsername())) {
					apartmentDao.Delete(apartment);
				}
			}
		}
		
		user.setBlocked(true);
		userDao.Edit(user);
		return user;
	}
	
	@POST
	@Path("/logIn")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public UserDAO logIn(User user) throws IOException{
		User retVal = new User();
		UserDAO dao=(UserDAO) sc.getAttribute("userDAO");
		User findUser=dao.LogIn(user.getUsername(), user.getPassword());
		
		if(findUser==null || findUser.blocked) {
			throw new BadRequestException();
		}
		String token = generateToken(findUser, Token_Time);
		String refreshToken = generateToken(findUser, Refresh_Token);
		dao.user = findUser;
		dao.authorizedToken = token;
		dao.refreshToken = token;
		return dao;
	}

	@POST
	@Secured({UserType.Admin, UserType.Guest, UserType.Host})
	@Path("/edit")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public User edit(User user) throws JsonIOException, JsonSyntaxException, IOException {
		UserDAO dao=(UserDAO) sc.getAttribute("userDAO");
		User editUser=dao.Edit(user);
		return editUser;
	}

	@GET
	@Secured({UserType.Admin, UserType.Host})
	@Path("/getUsers")
	@Produces(MediaType.APPLICATION_JSON)
	public List<User> getAllUsers(@Context SecurityContext securityContext) throws JsonIOException, JsonSyntaxException, IOException{	
		UserDAO dao=(UserDAO) sc.getAttribute("userDAO");
		ReservationDAO resDao = new ReservationDAO();
		Principal principal = securityContext.getUserPrincipal();
		String username = principal.getName();
		User user = dao.getUserByUsername(username);
		if(user.getRole() == UserType.Admin)
			return dao.GetAll();
		else {
			ArrayList<User> users = new ArrayList<User>();
			for(Reservation res : resDao.GetAll()) {
				if(res.getApartment().getHost().getUsername().equals(username)) {
					int flag = 0;
					for(User u : users) {
						if(u.getUsername().equals(res.guest.username)) {
							flag = 1;
							break;
						}
					}
					if(flag != 1)
						users.add(res.getGuest());
				}
			}
			return users;
		}
	}
	
	private String generateToken(User user, int time) {
		try {
			 Algorithm algorithm = Algorithm.HMAC256(JWT_TOKEN_KEY);
		        Date expirationDate = Date.from(ZonedDateTime.now().plusHours(time).toInstant());
		        Date issuedAt = Date.from(ZonedDateTime.now().toInstant());
		        return JWT.create()
		                // Issue date.
		                .withIssuedAt(issuedAt)
		                // Expiration date.
		                .withExpiresAt(expirationDate)
		                // User id - here we can put anything we want, but for the example userId is appropriate.
		                .withClaim("username", user.getUsername())
		                // Issuer of the token.
		                .withIssuer("jwtauth") 
		                
		                .withAudience("audience")
		                // And the signing algorithm.
		                .sign(algorithm); 
		} catch (JWTCreationException  e) {
	        e.printStackTrace();
	    }
		return null;
	}
	
}
