(function(exports) {

    var MyPresentation = function(my, data) {
        var self = this;

        self.favorite = ko.observable(data.favorite?true:false);

        self.favorite.subscribe(function(newValue) {
            data.favorite = newValue;
            my.save();
        });
    }

    var My = function(data) {
        var self = this;

        function loadData(data) {
            self.data = data;
        }


        function save() {
            postJSON('/my', self.data);
            localStorage.setItem('/my', JSON.stringify(self.data));
        }

        loadData(data);

        self.save = save;

        self.presentations = {};

        self.presentation = function(eventId, presId) {
            if (!eventId || !presId) {
                return new MyPresentation(self, {});
            }
            if (!self.presentations[eventId + '/' + presId]) {
                var event = self.data.events[eventId];
                if (!event) {
                    event = { id: eventId, presentations: {} };
                    self.data.events[eventId] = event;
                }
                var pres = event.presentations[presId];
                if (!pres) {
                    pres = { id: presId, favorite: false };
                    event.presentations[presId] = pres;
                }
                self.presentations[eventId + '/' + presId] = new MyPresentation(self, pres);
            }
            return self.presentations[eventId + '/' + presId];
        }
    }

    var User = function(data) {
        var self = this;
        self.id = ko.observable(data.id);
        self.name = ko.observable(data.name);

        self.my = ko.observable(new My({events: {}}));

        getJSON('/my', function(data) {
            self.my(new My(data))
        });
    }
    User.current = ko.observable(new User({name: 'a@' + models.Device.current().id()}));
    models.Device.current().id.subscribe(function(newValue) {
        User.current().name('a@' + newValue);
        console.log('User is ' + User.current().name());
    });
    console.log('User is ' + User.current().name());

    exports.models = exports.models || {};
    exports.models.User = User;
})(window);