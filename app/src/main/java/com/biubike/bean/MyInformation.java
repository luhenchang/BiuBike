package com.biubike.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ls on 2017/11/22.
 */

public class MyInformation implements Serializable {
    private static final long serialVersionUID = -758459502806858414L;

    /**
     * status : 0
     * total : 3
     * size : 3
     * contents : [{"tags":"王飞医生","uid":2384012289,"province":"天津市","geotable_id":179931,"district":"河西区","create_time":1511250462,"city":"天津市","location":[117.256251,39.070716],"address":"天津市河西区洞庭路41号-北","title":"海翔公寓","coord_type":3,"type":0,"distance":0,"weight":0},{"tags":"王意","uid":2384012573,"province":"天津市","geotable_id":179931,"district":"河西区","create_time":1511250490,"city":"天津市","location":[117.255959,39.07038],"address":"天津市河西区洞庭路41号","title":"海翔公寓","coord_type":3,"type":0,"distance":0,"weight":0},{"tags":"张老师","uid":2384012804,"province":"天津市","geotable_id":179931,"district":"河西区","create_time":1511250510,"city":"天津市","location":[117.255393,39.070174],"address":"天津市河西区洞庭路41号","title":"天津市中津进口汽车维修有限公司","coord_type":3,"type":0,"distance":0,"weight":0}]
     */

    private int status;
    private int total;
    private int size;
    public List<ContentsEntity> contents;

    public void setStatus(int status) {
        this.status = status;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setContents(List<ContentsEntity> contents) {
        this.contents = contents;
    }

    public int getStatus() {
        return status;
    }

    public int getTotal() {
        return total;
    }

    public int getSize() {
        return size;
    }

    public List<ContentsEntity> getContents() {
        return contents;
    }

    public static class ContentsEntity implements Serializable {
        /**
         * tags : 王飞医生
         * uid : 2384012289
         * province : 天津市
         * geotable_id : 179931
         * district : 河西区
         * create_time : 1511250462
         * city : 天津市
         * location : [117.256251,39.070716]
         * address : 天津市河西区洞庭路41号-北
         * title : 海翔公寓
         * coord_type : 3
         * type : 0
         * distance : 0
         * weight : 0
         */
        public String search_name;
        private String tags;
        private long uid;
        private String province;
        private int geotable_id;
        private String district;
        private int create_time;
        private String city;
        private String address;
        private String title;
        private int coord_type;
        private int type;
        private int distance;
        private int weight;
        private List<Double> location;

        public void setTags(String tags) {
            this.tags = tags;
        }

        public void setUid(long uid) {
            this.uid = uid;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public void setGeotable_id(int geotable_id) {
            this.geotable_id = geotable_id;
        }

        public void setDistrict(String district) {
            this.district = district;
        }

        public void setCreate_time(int create_time) {
            this.create_time = create_time;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setCoord_type(int coord_type) {
            this.coord_type = coord_type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public void setDistance(int distance) {
            this.distance = distance;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public void setLocation(List<Double> location) {
            this.location = location;
        }

        public String getTags() {
            return tags;
        }

        public long getUid() {
            return uid;
        }

        public String getProvince() {
            return province;
        }

        public int getGeotable_id() {
            return geotable_id;
        }

        public String getDistrict() {
            return district;
        }

        public int getCreate_time() {
            return create_time;
        }

        public String getCity() {
            return city;
        }

        public String getAddress() {
            return address;
        }

        public String getTitle() {
            return title;
        }

        public int getCoord_type() {
            return coord_type;
        }

        public int getType() {
            return type;
        }

        public int getDistance() {
            return distance;
        }

        public int getWeight() {
            return weight;
        }

        public List<Double> getLocation() {
            return location;
        }

        @Override
        public String toString() {
            return "ContentsEntity{" +
                    "tags='" + tags + '\'' +
                    ", uid=" + uid +
                    ", province='" + province + '\'' +
                    ", geotable_id=" + geotable_id +
                    ", district='" + district + '\'' +
                    ", create_time=" + create_time +
                    ", city='" + city + '\'' +
                    ", address='" + address + '\'' +
                    ", title='" + title + '\'' +
                    ", coord_type=" + coord_type +
                    ", type=" + type +
                    ", distance=" + distance +
                    ", weight=" + weight +
                    ", location=" + location +
                    '}';
        }
    }
}
