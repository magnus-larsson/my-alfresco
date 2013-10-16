var peopleLibrary = {

   sortPersonList: function(list) {
      var self = this;
      
      list.sort(function(person1, person2) {
         var result = 0;
         
         var firstname1 = self.getPersonProperty(person1, 'cm:firstName');
         var firstname2 = self.getPersonProperty(person2, 'cm:firstName');
         var lastname1 = self.getPersonProperty(person1, 'cm:lastName');
         var lastname2 = self.getPersonProperty(person2, 'cm:lastName');
         
         if (firstname1 != firstname2) {
            result = firstname1 > firstname2 ? 1 : -1;
         }

         if (lastname1 != lastname2) {
            result = lastname1 > lastname2 ? 1 : -1;
         }

         return result;
      });
      
      return list;
   },

   getPersonProperty: function(person, property) {
      var value = person.properties[property];
      
      if (!value) {
         value = "";
      }
      
      return value.toLowerCase();
   }
      
};
