function orderAlpha(){$("#order > ol > li.alpha").removeClass("out").addClass("in"),$("#order > ol > li.inherit").removeClass("in").addClass("out"),$("#order > ol > li.group").removeClass("in").addClass("out"),$("#template > div.parent").hide(),$("#template > div.conversion").hide(),$("#mbrsel > div.ancestors").show(),filter()}function orderInherit(){$("#order > ol > li.inherit").removeClass("out").addClass("in"),$("#order > ol > li.alpha").removeClass("in").addClass("out"),$("#order > ol > li.group").removeClass("in").addClass("out"),$("#template > div.parent").show(),$("#template > div.conversion").show(),$("#mbrsel > div.ancestors").hide(),filter()}function orderGroup(){$("#order > ol > li.group").removeClass("out").addClass("in"),$("#order > ol > li.alpha").removeClass("in").addClass("out"),$("#order > ol > li.inherit").removeClass("in").addClass("out"),$("#template > div.parent").hide(),$("#template > div.conversion").hide(),$("#mbrsel > div.ancestors").show(),filter()}function initInherit(){var e=new Object,i=new Object;$("#inheritedMembers > div.parent").each((function(){e[$(this).attr("name")]=$(this)})),$("#inheritedMembers > div.conversion").each((function(){e[$(this).attr("name")]=$(this)})),$("#groupedMembers > div.group").each((function(){i[$(this).attr("name")]=$(this)})),$("#types > ol > li").add("#deprecatedTypes > ol > li").each((function(){var t=$(this);this.mbrText=t.find("> .fullcomment .cmt").text();var s=t.attr("name"),l=s.slice(0,s.indexOf("#")),r=(s.slice(s.indexOf("#")+1),e[l]);null!=r&&(0==(o=$("> .types > ol",r)).length&&(r.append("<div class='types members'><h3>Type Members</h3><ol></ol></div>"),o=$("> .types > ol",r)),(n=t.clone())[0].mbrText=this.mbrText,o.append(n));var o,n,a=t.attr("group"),d=i[a];null!=d&&(0==(o=$("> .types > ol",d)).length&&(d.append("<div class='types members'><ol></ol></div>"),o=$("> .types > ol",d)),(n=t.clone())[0].mbrText=this.mbrText,o.append(n))})),$(".values > ol > li").each((function(){var t=$(this);this.mbrText=t.find("> .fullcomment .cmt").text();var s=t.attr("name"),l=s.slice(0,s.indexOf("#")),r=(s.slice(s.indexOf("#")+1),e[l]);null!=r&&(0==(o=$("> .values > ol",r)).length&&(r.append("<div class='values members'><h3>Value Members</h3><ol></ol></div>"),o=$("> .values > ol",r)),(n=t.clone())[0].mbrText=this.mbrText,o.append(n));var o,n,a=t.attr("group"),d=i[a];null!=d&&(0==(o=$("> .values > ol",d)).length&&(d.append("<div class='values members'><ol></ol></div>"),o=$("> .values > ol",d)),(n=t.clone())[0].mbrText=this.mbrText,o.append(n))})),$("#inheritedMembers > div.parent").each((function(){0==$("> div.members",this).length&&$(this).remove()})),$("#inheritedMembers > div.conversion").each((function(){0==$("> div.members",this).length&&$(this).remove()})),$("#groupedMembers > div.group").each((function(){0==$("> div.members",this).length&&$(this).remove()}))}function filter(){var e=$.trim($("#memberfilter input").val()).toLowerCase();e=e.replace(/[-[\]{}()*+?.,\\^$|#]/g,"\\$&").replace(/\s+/g,"|");var i,t=new RegExp(e,"i"),s=$("#visbl > ol > li.public").hasClass("in"),l=$("#visbl > ol > li.protected").hasClass("in"),r=$("#visbl > ol > li.private").hasClass("in"),o=$("#order > ol > li.alpha").hasClass("in"),n=$("#order > ol > li.inherit").hasClass("in"),a=$("#order > ol > li.group").hasClass("in"),d=(n?$("#linearization > li").slice(1):$("#linearization > li.out")).map((function(){return $(this).attr("name")})).get(),c=(n?$("#implicits > li"):$("#implicits > li.out")).map((function(){return $(this).attr("name")})).get();function h(){var o=!1,n=$(this);n.find("> ol > li").each((function(){var n=$(this),a=n.attr("visbl");if(s||"pub"!=a)if(l||"prt"!=a)if(r||"prv"!=a){var h=n.attr("name");if(i){var u=h.indexOf("#");u<0&&(u=h.lastIndexOf("."));for(var m=h.slice(0,u),v=0;v<d.length;v++)if(d[v]==m)return void n.hide();for(v=0;v<c.length;v++)if(c[v]==m)return void n.hide()}!e||t.test(h)||t.test(this.mbrText)?(n.show(),o=!0):n.hide()}else n.hide();else n.hide();else n.hide()})),o?n.show():n.hide()}return o?($("#allMembers").show(),$("#inheritedMembers").hide(),$("#groupedMembers").hide(),i=!0,$("#allMembers > .members").each(h)):a?($("#groupedMembers").show(),$("#inheritedMembers").hide(),$("#allMembers").hide(),i=!0,$("#groupedMembers  > .group > .members").each(h),$("#groupedMembers  > div.group").each((function(){$(this).show(),0==$("> div.members",this).not(":hidden").length?$(this).hide():$(this).show()}))):n&&($("#inheritedMembers").show(),$("#groupedMembers").hide(),$("#allMembers").hide(),i=!1,$("#inheritedMembers > .parent > .members").each(h),$("#inheritedMembers > .conversion > .members").each(h)),!1}function isMobile(){return/Android|webOS|Mobi|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)}$(document).ready((function(){var e=$("div#subpackage-spacer").width()+1+"px";$("div#packages > ul > li.current").on("click",(function(){$("div#subpackage-spacer").css({width:e}),$("li.current-entities").toggle()}));var i={visibility:{publicFilter:$("#visbl").find("> ol > li.public"),protectedFilter:$("#visbl").find("> ol > li.protected"),privateFilter:$("#visbl").find("> ol > li.private")}};function t(){$(this).toggleClass("in").toggleClass("out"),filter()}function s(e){var t=e.parent(),s=t.attr("name"),l=/^([^#]*)(#.*)?$/gi.exec(s)[1];"prt"==t.attr("visbl")&&i.visibility.privateFilter.removeClass("out").addClass("in"),l&&$("#filterby li.out[name='"+l+"']").removeClass("out").addClass("in"),filter(),t.addClass("selected"),n(t),$("#content-scroll-container").animate({scrollTop:$("#content-scroll-container").scrollTop()+t.offset().top-$("#search").height()-23},1e3)}i.visibility.publicFilter.on("click",t),i.visibility.protectedFilter.on("click",t),i.visibility.privateFilter.on("click",t);var l=function(e){return"scala.Any"==e||"scala.AnyRef"==e},r=function(e){return"true"==$(e).attr("data-hidden")};$("#linearization li").slice(1).filter((function(){return l($(this).attr("name"))})).removeClass("in").addClass("out"),$("#implicits li").filter((function(){return r(this)})).removeClass("in").addClass("out"),$("#memberfilter > i.arrow").on("click",(function(){$(this).toggleClass("rotate"),$("#filterby").toggle()})),filter();var o=$("#memberfilter input");function n(e){if($("#template li.selected").removeClass("selected"),!e.is("[fullcomment=no]")){e.toggleClass("open");var i=e.find(".modifier_kind"),t=e.find(".shortcomment"),s=e.find(".fullcomment"),l=$(":visible",s);i.toggleClass("closed").toggleClass("opened"),l.length>0?isMobile()?(s.hide(),t.show()):(t.slideDown(100),s.slideUp(100)):isMobile()?(t.hide(),s.show()):(t.slideUp(100),s.slideDown(100))}}function a(e){e.toggleClass("open");var i=$(".hiddenContent",e);i.is(":visible")?isMobile()?i.hide():i.slideUp(100):(setTimeout((function(){i.trigger("beforeShow")}),100),isMobile()?i.show():i.slideDown(100))}function d(e){var i=e.replace("#",""),t="#"+i.replace(/([;&,\.\+\*\~':"\!\^#$%@\[\]\(\)=<>\|])/g,"\\$1");return $(t)}if(o.on("keyup",(function(e){switch(e.keyCode){case 27:o.val(""),filter(!0);break;case 38:o.val(""),filter(!1),window.scrollTo(0,$("body").offset().top),o.trigger("focus");break;case 33:case 34:o.val(""),filter(!1);break;default:window.scrollTo(0,$("#mbrsel").offset().top-130),filter(!0)}})),o.on("focus",(function(e){o.trigger("select")})),$("#memberfilter > .clear").on("click",(function(){$("#memberfilter input").val(""),$(this).hide(),filter()})),$(document).on("keydown",(function(e){if(9==e.keyCode)return $("#index-input",window.parent.document).trigger("focus"),o.val(""),!1})),$("#linearization li").on("click",(function(){$(this).hasClass("in")?($(this).removeClass("in"),$(this).addClass("out")):$(this).hasClass("out")&&($(this).removeClass("out"),$(this).addClass("in")),filter()})),$("#implicits li").on("click",(function(){$(this).hasClass("in")?($(this).removeClass("in"),$(this).addClass("out")):$(this).hasClass("out")&&($(this).removeClass("out"),$(this).addClass("in")),filter()})),$("#mbrsel > div > div.ancestors > ol > li.hideall").on("click",(function(){$("#linearization li.in").removeClass("in").addClass("out"),$("#linearization li:first").removeClass("out").addClass("in"),$("#implicits li.in").removeClass("in").addClass("out"),$(this).hasClass("out")&&$("#mbrsel > div > div.ancestors > ol > li.showall").hasClass("in")&&($(this).removeClass("out").addClass("in"),$("#mbrsel > div > div.ancestors > ol > li.showall").removeClass("in").addClass("out")),filter()})),$("#mbrsel > div > div.ancestors > ol > li.showall").on("click",(function(){$("#linearization li.out").filter((function(){return!l($(this).attr("name"))})).removeClass("out").addClass("in"),$("#implicits li.out").filter((function(){return!r(this)})).removeClass("out").addClass("in"),$(this).hasClass("out")&&$("#mbrsel > div > div.ancestors > ol > li.hideall").hasClass("in")&&($(this).removeClass("out").addClass("in"),$("#mbrsel > div > div.ancestors > ol > li.hideall").removeClass("in").addClass("out")),filter()})),$("#order > ol > li.alpha").on("click",(function(){$(this).hasClass("out")&&orderAlpha()})),$("#order > ol > li.inherit").on("click",(function(){$(this).hasClass("out")&&orderInherit()})),$("#order > ol > li.group").on("click",(function(){$(this).hasClass("out")&&orderGroup()})),$("#groupedMembers").hide(),initInherit(),$(".extype").add(".defval").each((function(e,i){var t=$(i);t.attr("title",t.attr("name"))})),$("#template li[fullComment=yes] .modifier_kind").addClass("closed"),$("#template li[fullComment=yes]").on("click",(function(){window.getSelection().toString()||n($(this))})),$(".toggle").on("click",(function(){if(a($(this).parent()),$(this).parent().hasClass("full-signature-block"))return!1})),1==$("#order > ol > li.group").length&&orderGroup(),window.location.hash){var c=d(decodeURIComponent(window.location.hash));c.length>0&&(c.hasClass("toggleContainer")?a(c):s(c))}$("#template span.permalink").on("click",(function(e){e.preventDefault();var i=$("a",this).attr("href");if(-1!=i.indexOf("#")){var t=i.split("#").pop();try{window.history.pushState({},"","#"+t)}catch(e){location.hash=t}s(d(t))}return!1})),$("#mbrsel-input").on("input",(function(){$(this).val().length>0?$("#memberfilter > .clear").show():$("#memberfilter > .clear").hide()}))}));