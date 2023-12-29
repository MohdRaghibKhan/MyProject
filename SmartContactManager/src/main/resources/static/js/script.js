console.log("this is script file");

const toggleSidebar = () => {

	if ($('.sidebar').is(":visible")) {
		$(".sidebar").css("display", "none");
		$(".content").css("margin-left", "0%");
	} else {
		$(".sidebar").css("display", "block");
		$(".content").css("margin-left", "20%");
	}
};
const search = () => {
	let query = $("#search-input").val();
	console.log(query);

	if (query === '') {
		$(".search-result").hide();
	} else {
		console.log(query);
		/*sending request to server */
		let url = `http://localhost:9999/search/${query}`;

		fetch(url)
			.then(response => {
				return response.json(); // Invoke the json() method
			})
			.then((data) => {
				/* Process the JSON data */
			let text=`<div class='list-group'>`;
			data.forEach(contact =>{
				text+=`<a href='/user/contact/${contact.c_id}' class='list-group-item list-group-item-action' >${contact.name}</a> `
			});
			text+=`</div>`
			$(".search-result").html(text);
			})
			.catch(error => {
				console.error('Error:', error);
			});

		$(".search-result").show();
}

	
}