Vue.component('apartment-details', {
	data : function () {
		return  {
			apartment : !!localStorage.getItem("apartment") ? JSON.parse(localStorage.getItem("apartment")) : {},
			comForm : "none"
		}
	},
	template: `
	<div>
		</br>
		</br>
		
		<div style="display : inline">
			<h3 style="float : left; margin-left : 20%">Smestaj {{apartment.name}}</h3>
			<button class="btn btn-primary" v-on:click="backToSearch()" style="float: right; margin-right : 10%" >Povratak na pretragu </button>
		</div>
		<br>
		</br>
		<br>
		<div style="display : flex; align-items : center; justify-content : center; height : auto; width : 100%;">
			<div>
				<img style="margin-left : 5%; border-radius: 20%; padding: 10px;"  width="350" height="300" src="https://image.shutterstock.com/image-photo/bright-spring-view-cameo-island-260nw-1048185397.jpg">
			</div>
			<div style="margin-right: 5%; margin-left : 5%">
				<p class="card-text" style="font-family: Arial, Helvetica, sans-serif;"> </br> <b> Lokacija : </b> {{apartment.location.adress.street}} {{apartment.location.adress.numberOfStreet}}, {{apartment.location.adress.city}} {{apartment.location.adress.postNumber}} </br>		 	
	    			<b> Geografska sirina i duzina : </b> {{apartment.location.latitude}} {{apartment.location.longitude}} </br>
	    			<b> Cena po noci : </b> {{apartment.pricePerNight}}
	    		</p>
	    		<!--Opis apartmana-->
	    		<p 	style="word-wrap: break-word;"> "AAAAAAAAAAA AaaaaAAAAAAAAAAAA AAAAAAAAAAAAAAAA AAAAAA AAAAAAAAAAAAAA a    aaaaaa aaaaa aaaaa aaaaa aaaa"</p>
			</div>
		</div>
		
		<!--<p> Domacin smestaja : {{apartment.host.name}} {{apartment.host.surname}} </p>-->
		<br>
		
		<div class="container">
			<h4> Dodaci </h4>
			<div style="display : inline" v-for="a in apartment.amenities">
				 <p style="display : inline"> <b> {{a.name}}  </b>  </p> 
			</div>
		</div>
		
		<br>
		<br>
		<div class="container">
		  	<h4>Komentari: </h4>
		  	</br>
		  		<div class="comments-list" v-for="com in apartment.comments" v-if="com.isApproved">
		  			<div class="media" style="display : inline ">
		  				 <p class="float-right" >Ocena : {{com.grade}} </p>
		  				<div class="media-body"> 
		  				<h4 class="media-heading user_name" style="font-size: 15px">Jovan Jelicki</h4>
		  					{{com.text}}
		  				</div>
		  			</div>
		  			<hr>
		  		</div>
		  	<button id="buttonComment" class="btn btn-primary" v-on:click="commentForm()"> Ostavi komentar </button>
			<div class="form-group" v-bind:style="{display : comForm}">
				<textarea class="form-control" rows="5" cols="30" placeholder="Ostavite komentar..." id="comment"></textarea>
				<br>
				<button class="btn btn-primary" v-on:click="leaveComment()"> Prosledi komentar </button>
			</div>
		</div>

	
	</div>
	
	`,
	methods : {
		backToSearch : function () {
			location.replace('#/');
		},
		commentForm : function() {
			this.comForm = "inline";
			buttonComment.style.display = "none";
		},
		leaveComment : function () {
			this.comForm = "none";
			buttonComment.style.display = "inline";
			this.apartment.comments.push({"text" : comment.value, "apartment" : this.apartment.id, "grade" : "4"});
			axios
			.post("rest/apartmentService/edit", this.apartment)
			.then(response => {
				this.apartment = response.data;
				var apartments = JSON.parse(localStorage.getItem("apartments"));
				for(a of apartments){
					if(a.id == this.apartment.id)
						a.comments = this.apartment.comments;
				}
				localStorage.setItem("apartments",  JSON.stringify(apartments));
			})
		}
		
	}

})