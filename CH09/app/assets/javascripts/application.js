$(document).ready(function () {
    $('#button').on('click', function () {
        jsRoutes.controllers.Application.text().ajax({
            success: function(text) {
                $('#text').text(text);
            }, error: function() {
                alert('Uh oh');
            }
        });
    });
});
