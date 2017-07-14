# SqliteManager
对sqlite的封装，使用对象建表，异步插入数据，查询数据，更新数据，删除数据

1.创建数据库<br>

<pre>
    <code>
        //在data/data/package/databases/下创建
        String path="info.db";
        //如果是4.2或者4.4path=getFilesDir().getAbsolutePath()+"meituan.db"
        DbManager.dbManager.getInstans(getApplicationContext()).OpenDb(path);
        //在sd卡上创建(6.0后注意检查和申请权限)
        String path2=Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/info.db";
        DbManager.dbManager.getInstans(getApplicationContext()).OpenDb(path2);
    </code>
</pre>

2.创建数据表<br>
 2.1将数据表抽象成类 <code>这个类的属性都使用引用类型<code><br>
 <pre>
     <code>
            public class Teacher implements DbManager.TableModel{

                @TableField//表示这个成员是表中的字段，如果没有此注解就不是字段
                @FieldType(value ="Integer")//字段的类型
                @FieldConstraint(value = {"primary key"})//约束
                public Integer _id;
                @TableField
                @FieldType(value ="varchar(10)")
                @FieldConstraint(value = {"not null"})
                public String name;
                @TableField
                @FieldType(value ="Integer")
                public Integer age;
                @TableField
                @FieldType(value ="text")
                public List<String> age;
                public String friend;
                public String [] freads;


            }

     </code>
 </pre>
  2.2 打开数据库，创建表<br>
  <pre>
    <code>
            DbManager.dbManager.createTable(Teacher.class, new DbCallBack<Boolean>() {
                        @Override
                        public void before() {
                            Log.i(TAG, "before: 建表前");
                        }

                        @Override
                        public void success(Boolean result) {
                            Log.i(TAG, "success: 建表成功" + result);
                        }

                        @Override
                        public void failure(Throwable error) {
                            Log.i(TAG, "failure: 建表失败" + error.getMessage());
                        }

                        @Override
                        public void finish() {
                            Log.i(TAG, "finish: 建表完成");
                        }
                    });
    </code>
  </pre>

  3.插入数据<p></p>
  <pre>
    <code>
        Teacher teacher = new Teacher();
                teacher.friend = "bbbc";
                teacher.name = "赵云";
                DbManager.dbManager.insert(teacher, new DbCallBack<Long>() {
                    @Override
                    public void before() {
                        Log.i(TAG, "before: 开始插入数据");
                    }

                    @Override
                    public void success(Long row) {
                        Log.i(TAG, "success: " + row);
                    }

                    @Override
                    public void failure(Throwable error) {
                        Log.i(TAG, "failure: 插入失败============" + error.getMessage());
                    }

                    @Override
                    public void finish() {
                        Log.i(TAG, "finish: 插入完成");
                    }
                });
    </code>
  </pre>

  4.更新数据<p></p>
  <pre>
    <code>
        Teacher teacher = new Teacher();
                teacher.name = "王思聪";
                teacher.age = 30;

                DbManager.dbManager.update(teacher, "_id=?", new String[]{"10"}, new DbCallBack<Integer>() {
                    @Override
                    public void before() {
                        Log.i(TAG, "before: 更新数据");
                    }

                    @Override
                    public void success(Integer result) {
                        Log.i(TAG, "success: 更新成功" + result);
                    }

                    @Override
                    public void failure(Throwable error) {
                        Log.i(TAG, "failure: 更新失败" + error.getMessage());
                    }

                    @Override
                    public void finish() {
                        Log.i(TAG, "finish: 更新完成");
                    }
                });
    </code>
  </pre>

  5.删除数据<p></p>
  <pre>
      <code>
            DbManager.dbManager.delete(Teacher.class, "name=?", new String[]{"王思聪"}, new DbCallBack<Integer>() {
                        @Override
                        public void before() {
                            Log.i(TAG, "before: 删除开始");
                        }

                        @Override
                        public void success(Integer result) {
                            Log.i(TAG, "before: 删除成功" + result);
                        }

                        @Override
                        public void failure(Throwable error) {
                            Log.i(TAG, "failure: 删除失败" + error.getMessage());
                        }

                        @Override
                        public void finish() {
                            Log.i(TAG, "finish: 删除完毕");
                        }
                    });
      </code>
  </pre>


  6.查询数据<p></p>
  <pre>
      <code>
            Teacher teacher = new Teacher();
                    //设置的值表示这些是要查询的字段
                    teacher._id = 0;//设置的值无实际意义
                    teacher.name = "?";//设置的值无实际意义

                    DbManager.dbManager.query(teacher, "_id DESC", new DbCallBack<List<Teacher>>() {
                        @Override
                        public void before() {
                            Log.i(TAG, "before: 开始查询");
                        }

                        @Override
                        public void success(List<Teacher> result) {
                            if (result == null) {
                                return;
                            }
                            Log.i(TAG, "success: 查询成功");
                            for (int i = 0; i < result.size(); i++) {
                                Log.i(TAG, "success: =======" + result.get(i).toString());
                            }
                        }

                        @Override
                        public void failure(Throwable error) {
                            Log.i(TAG, "failure: 失败了" + error.getMessage());
                        }

                        @Override
                        public void finish() {
                            Log.i(TAG, "finish: 查询完毕");
                        }
                    });
      </code>
  </pre>

  7.最后在合适的时候调用<code>DbManager.dbManager.unscribe();//取消所有rx的订阅</code>
