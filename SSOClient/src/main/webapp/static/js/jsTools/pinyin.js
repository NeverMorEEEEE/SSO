
// ��ȡƴ������ĸ
function getPinYin(c) {
execScript("tmp=asc(\""+c+"\")", "vbscript");
tmp = 65536 + tmp;
if(tmp>=45217 && tmp<=45252) return "A";
if(tmp>=45253 && tmp<=45760) return "B";
if(tmp>=45761 && tmp<=46317) return "C";
if(tmp>=46318 && tmp<=46825) return "D";
if(tmp>=46826 && tmp<=47009) return "E";
if(tmp>=47010 && tmp<=47296) return "F";
if((tmp>=47297 && tmp<=47613) || (tmp == 63193)) return "G";
if(tmp>=47614 && tmp<=48118) return "H";
if(tmp>=48119 && tmp<=49061) return "J";
if(tmp>=49062 && tmp<=49323) return "K";
if(tmp>=49324 && tmp<=49895) return "L";
if(tmp>=49896 && tmp<=50370) return "M";
if(tmp>=50371 && tmp<=50613) return "N";
if(tmp>=50614 && tmp<=50621) return "O";
if(tmp>=50622 && tmp<=50905) return "P";
if(tmp>=50906 && tmp<=51386) return "Q";
if(tmp>=51387 && tmp<=51445) return "R";
if(tmp>=51446 && tmp<=52217) return "S";
if(tmp>=52218 && tmp<=52697) return "T";
if(tmp>=52698 && tmp<=52979) return "W";
if(tmp>=52980 && tmp<=53688) return "X";
if(tmp>=53689 && tmp<=54480) return "Y";
if(tmp>=54481 && tmp<=62289) return "Z";
return c.charAt(0);
}
// select helper
SelectHelper = new function() {
// ��ʼ��
this.init = function() {
document.attachEvent("onkeypress", function() {
var elm = event.srcElement;
if (elm.tagName == "SELECT"
&& elm.className.indexOf("SelectHelper") == -1) {
elm.className += "SelectHelper";
elm.attachEvent("onkeypress", SelectHelper.getNextKeyItem);
elm.fireEvent("onkeypress", event);
}
});
}
// ��ȡѡ���ı������ַ�
function getItemKeyChar(option) {
return option.text.charAt(0).toUpperCase();
}
// ���Ҳ�ѡ��ƥ���ѡ��
this.getNextKeyItem = function() {
var elm = event.srcElement;
var index = elm.selectedIndex + 1;
do {
if (index == elm.length) index = 0;
if (index == elm.selectedIndex) return false; // δ�ҵ�ƥ���ѡ����˳�
} while (key2Char(event.keyCode) != getPinYin(getItemKeyChar(elm.options[index++])));
elm.selectedIndex = index - 1; // ѡ��ƥ���ѡ��
return false; // ȡ��ԭ�е�ѡ����
}
};
/**
* ���ؼ����¼���Ӧ����ĸ������
* a-z: 97 -> 122
* A-Z: 65 -> 90
* 0-9: 48 -> 57
*/
function key2Char(key) {
var s = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
if (key >= 97 && key <= 122) return s.charAt(key - 97);
if (key >= 65 && key <= 90) return s.charAt(key - 65);
if (key >= 48 && key <= 57) return "" + (key - 48);
return null;
}