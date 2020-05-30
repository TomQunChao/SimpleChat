var vm = new Vue({
  el: "#app",
  data: {
    form: {
      name: '',
      password: ''
    },
    info: {
      title_info: "Successful",
      login_register_info: "Login or Register Success",
      visible: false
    }
  },
  methods: {
    onSubmit() {
      console.log('submit! ' + this.form.name + this.form.password);
      this.info.visible = true
      window.location.href = "index.html"
    },
    login() {
      let userName = this.form.name;
      let password = this.form.password;
      console.log(userName,password)
      xmlHttp = new XMLHttpRequest();

      xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
          console.log(xmlHttp.responseText);
          let obj = JSON.parse(xmlHttp.responseText);
          console.log(obj);
          vm.info.title_info=obj.type;
          vm.info.login_register_info=obj.data;
          vm.info.visible=true;
          if(obj.type=="success"){
            sessionStorage.setItem("user",userName);
            let data = obj.data;
            let rand = data[0];
            sessionStorage.setItem("rand",rand);
            window.location.href="../index.html";
          }
        }
      }

      xmlHttp.open("POST", "/chat/login_control", true);
      xmlHttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
      xmlHttp.send("username=" + userName + "&password=" +password);

    },
    register() {
      let userName = this.form.name;
      let password = this.form.password;
      xmlHttp = new XMLHttpRequest();

      xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
          console.log(xmlHttp.responseText);
          let obj = JSON.parse(xmlHttp.responseText);
          console.log(obj);
          vm.info.title_info=obj.type;
          vm.info.login_register_info=obj.data[0];
          vm.info.visible=true;
        }
      }

      xmlHttp.open("POST", "/chat/register_control", true);
      xmlHttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
      xmlHttp.send("username=" + userName + "&password="+password);
    }
  }
});