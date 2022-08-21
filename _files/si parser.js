var tag = document.getElementsByClassName("settings")
list = new Array()
var j = 0
for (var i = 0; i < tag.length; i++){
var url = tag[i].href
if (!url.includes('youtube.com') && !url.includes('wasd.tv')){
list[j] = url.replace(['https://www.twitch.tv/'], '')
j++
}
}
window.open().document.write(list.join('<br/>'))