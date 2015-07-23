(function (requirejs) {
    'use strict';
    requirejs.config({
        shim: {
            'jsRoutes': {
                deps: [],
                exports: 'jsRoutes'
            }
        },
        paths: {
            'jquery': ['../lib/jquery/jquery']
        }
    });

    requirejs.onError = function (err) {
        console.log(err);
    };

    require(['jquery'], function ($) {
        $(document).ready(function () {
            $('#button').on('click', function () {
                jsRoutes.controllers.Application.text().ajax({
                    success: function (text) {
                        $('#text').text(text);
                    }, error: function () {
                        alert('Uh oh');
                    }
                });
            });
        });
    });
})(requirejs);
