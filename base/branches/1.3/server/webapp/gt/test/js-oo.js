/*****
*
*    Person constructor
*
*****/
function Person(first, last) {
    if ( arguments.length > 0 )
        this.init(first, last);
}

/*****
*
*    Person init
*
*****/
Person.prototype.init = function(first, last) {
    this.first = first;
    this.last  = last;
};

/*****
*
*    Person toString
*
*****/
Person.prototype.toString = function() {
    return this.first + "," + this.last;
};


/*****
*
*    Setup Employee inheritance
*
*****/
Employee.prototype = new Person();
Employee.prototype.constructor = Employee;
Employee.superclass = Person.prototype;

/*****
*
*    Employee constructor
*
*****/
function Employee(first, last, id) {
    if ( arguments.length > 0 )
        this.init(first, last, id);
}

/*****
*
*    Employee init
*
*****/
Employee.prototype.init = function(first, last, id) {
    // Call superclass method
    Employee.superclass.init.call(this, first, last);

    // init properties
    this.id = id;
}

/*****
*
*    Employee toString
*
*****/
Employee.prototype.toString = function() {
    var name = Employee.superclass.toString.call(this);

    return this.id + ":" + name;
};


/*****
*
*    Setup Manager inheritance
*
*****/
Manager.prototype = new Employee;
Manager.prototype.constructor = Manager;
Manager.superclass = Employee.prototype;

/*****
*
*    Manager constructor
*
*****/
function Manager(first, last, id, department) {
    if ( arguments.length > 0 )
        this.init(first, last, id, department);
}

/*****
*
*    Manager init
*
*****/
Manager.prototype.init = function(first, last, id, department){
    // Call superclass method
    Manager.superclass.init.call(this, first, last, id);

    // init properties
    this.department = department;
}

/*****
*
*    Manager toString
*
*****/
Manager.prototype.toString = function() {
    var employee = Manager.superclass.toString.call(this);

    return employee + " manages " + this.department;
}