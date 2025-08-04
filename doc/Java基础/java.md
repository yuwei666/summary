测试请求

```java
@Test
public void test1() {
    RestTemplate restTemplate = new RestTemplate();
    MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
    form.add("username", "tom");
    form.add("age", "45");
    form.add("birthday", "1994-03-08");
    form.add("salary", "3000");

    String html = restTemplate.postForObject("http://localhost:8080/spring/user",form, String.class);
    Assert.assertNotNull(html);
}
```



#### 数组转集合

```
new ArrayList<>(Arrays.asList(arr));
```

