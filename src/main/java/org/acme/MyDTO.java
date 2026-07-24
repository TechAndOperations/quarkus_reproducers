package org.acme;


public class MyDTO {

    private String ref;

    private String uid;

    public MyDTO(String ref, String uid) {
        this.ref = ref;
        this.uid = uid;
    }

    public MyDTO() {
    }

    public String getRef() {
        return ref;
    }

    public String getUid() {
        return uid;
    }

    public MyDTO setRef(String ref) {
        this.ref = ref;
        return this;
    }

    public MyDTO setUid(String uid) {
        this.uid = uid;
        return this;
    }

    @Override
    public String toString() {
        return "MyDTO{" +
                "ref='" + ref + '\'' +
                ", uid='" + uid + '\'' +
                '}';
    }
}
