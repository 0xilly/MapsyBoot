package net.minecraftforge.mapsy.dao;

import javax.persistence.*;

@Entity
@Table(name = "fieldmapping")
public class FieldMapping {

   @Id
   @Column(name = "id")
   @GeneratedValue(strategy =  GenerationType.AUTO)
   private long id;

   @Column
   private String version;

   @Column(name = "class")
   private String klass;

   @Column
   private String obfName;

   @Column
   private String uName;

   public FieldMapping() {}

   public FieldMapping(long id, String version, String klass, String obfName, String uName) {
      this.id = id;
      this.version = version;
      this.klass = klass;
      this.obfName = obfName;
      this.uName = uName;
   }

   public long getId() {
      return id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public String getVersion() {
      return version;
   }

   public void setVersion(String version) {
      this.version = version;
   }

   public String getKlass() {
      return klass;
   }

   public void setKlass(String klass) {
      this.klass = klass;
   }

   public String getObfName() {
      return obfName;
   }

   public void setObfName(String obfName) {
      this.obfName = obfName;
   }

   public String getuName() {
      return uName;
   }

   public void setuName(String uName) {
      this.uName = uName;
   }
}
