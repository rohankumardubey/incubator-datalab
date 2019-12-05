/*jslint browser: true, white: true, plusplus: true, regexp: true, indent: 4, maxerr: 50, es5: true */
/*jshint multistr: true, latedef: nofunc */
/*global jQuery, $, Swiper*/

import $ from 'jquery';
import Swiper from "swiper";
import screen from './lib';
import lozad from "lozad";

// lazy loads elements with default selector as ".lozad"
const observer = lozad();
observer.observe();

var macSwiper,
    screenSliderTop,
    macNavigation = $('.controls-nav'),
    screenThumbsNavigation = $('.screen-slider-thumbs');
$(document).ready(function() {
    $('body').show();
    $('#msg').hide();
});

function stopMacVideos() {
    'use strict';
    $('.mac-wrap .swiper-slide video').each(function () {
        $(this).removeClass('is-playing')[0].pause();
    });
}

$(document).ready(function () {
    'use strict';

    document.getElementById('current_year').appendChild(document.createTextNode(new Date().getFullYear()));
    screenThumbsNavigation.on('click', 'div', function () {
        screenSliderTop.slideTo($(this).data('slide'));
        return false;
    });

    $('.mac-wrap').on('click', '.play-it', function () {
        stopMacVideos();
        $(this).prev().addClass('is-playing')[0].play();
        return false;
    });

    $('.we-solve').on('click', 'article > div', function () {
        var current = $(this),
            inx = current.index();
        if (!current.hasClass('is-opened')) {
            current.addClass('is-opened').siblings('div').removeClass('is-opened');
            $('.we-solve .problems-slider > div').slideUp(350);
            $('.we-solve .problems-slider > div:eq(' + inx + ')').slideDown({
                start: function () {
                    $(this).css({
                        display: 'flex'
                    })
                }
            });
        } else {
            $('.we-solve article > div').removeClass('is-opened');
            $('.we-solve .problems-slider > div').slideUp(350);
        }
        return false;
    });


    $('.get-started-nav, .get-started').on('click', function () {
        $('#details-overlay').css({
            'width': '100%',
            'left': 0,
            'opacity': 1
        });
    });

    $('.close-butt').on('click', function () {
        $('#details-overlay').css({
            'width': '0%',
            'left': '-1px',
            'opacity': 0
        });
    });
});

$(window).on('load', function () {
    'use strict';

    macSwiper = new Swiper('.mac-wrap .swiper-container', {
        lazyLoadingInPrevNext: true,
        preloadImages: true,
        lazyLoading: true,
        speed: 400,
        on: {
            init: function () {
                var self = this;
                macNavigation.on('click', 'mark', function () {
                    self.slideTo($(this).data('slide'));
                    return false;
                });
            },
        },
    });

    macSwiper.on('slideChange', function () {
        $('mark').removeClass('is-current-slide');
        $('[data-slide="' + this.activeIndex + '"]', macNavigation).addClass('is-current-slide');
        stopMacVideos();
    });

    screenSliderTop = new Swiper('.screen-slider-top', {
        navigation: {
            nextEl: '.screen-slider-top .swiper-button-next',
            prevEl: '.screen-slider-top .swiper-button-prev',
        },
        // autoHeight: true,
        height:550,
    });

    screenSliderTop.on('slideChange', function () {
        $('div', screenThumbsNavigation).removeClass('swiper-slide-active');
        $('[data-slide="' + this.activeIndex + '"]', screenThumbsNavigation).addClass('swiper-slide-active');
    });

    $(this).on('resize', function () {
        if (screen('width') > 767) {
            screenSliderTop.onlyExternal = true;
        } else {
            screenSliderTop.onlyExternal = false;
        }
    })

        .on('scroll', function () {
            var el = $('#wrap > section'),
                curr = el.filter(function (i, e) {
                    var tscroll = $(e).offset().top;
                    return ((screen() >= tscroll) && (screen() <= tscroll + $(e).outerHeight(true)));
                });

            el.removeClass('scroll-active');
            curr.addClass('scroll-active');
        });
});