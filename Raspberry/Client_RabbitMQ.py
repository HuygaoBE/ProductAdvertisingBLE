import pika, sys, os, time
import threading
import json
from datetime import datetime
def threadClient():
    def main():
        connection = pika.BlockingConnection(pika.ConnectionParameters(host='funnybunny-ui.7perldata.win'))
        channel = connection.channel()
        channel.queue_declare(queue='c3dca840-14b3-46af-856a-a2fb32a865be')

        def callback(ch, method, properties, body):

            str = body.decode('UTF-8')

            path_w = 'myfile.txt'
            with open(path_w, 'w+') as f:
                f.write(str)
                f.close()
            connection.close()

        channel.basic_consume(queue='c3dca840-14b3-46af-856a-a2fb32a865be', on_message_callback=callback, auto_ack=True)
        print('Waiting for messages...')
        channel.start_consuming()
#    main()

    class Store:
        def __init__(self, id, name, address, catalogs):
            self.id = id
            self.name = name
            self.address = address
            self.catalogs = catalogs

    class Catalog:
        def __init__(self, id, name, products):
            self.id = id
            self.name = name
            self.products = products

    class Product:
        def __init__(self, id, name, oldPrice, salePrice, quantity, unit, saleContent, saleDateFrom, saleDateTo):
            self.id = id
            self.name = name
            self.oldPrice = oldPrice
            self.salePrice = salePrice
            self.quantity = quantity
            self.unit = unit
            self.saleContent = saleContent
            self.saleDateFrom = saleDateFrom
            self.saleDateTo = saleDateTo

    #-----
    f= open("myfile.txt",'r')
    message = f.readline()
    f.close()

    def parseData(jsonObj):
        storeId = jsonObj["store_id"]
        storeName = jsonObj["store_name"]
        storeAddress = jsonObj["store_address"]

        catalogList = []
        for catalog in jsonObj["catalogs"]:
            catId = catalog["cat_id"]
            catName = catalog["cat_name"]
            productList = []
            for product in catalog["products"]:
                pro = Product(product["id"], product["name"], product["old_price"], product["sale_price"], product["quantity"],
                            product["unit"], product["saleContent"], product["saleDateFrom"], product["saleDateTo"])
                productList.append(pro)
            #Add a catalog to list
            catalogList.append(Catalog(catId, catName, productList))

            #print(catId + catName)

        return Store(storeId, storeName, storeAddress, catalogList)


        print(jsonObj["Store"])

    #Print store object value contains catalogs, products
    def printData(store):

        Sum_store = len(store.name) + len(store.address) + 1 + 9

        WriteStore = (store.name + '\n' + "Địa chỉ: " + store.address)

        Sum_product_lv1 = 0
        WriteProduct = ''
        for catalog in store.catalogs:

            Sum_product_lv2 = 0
            Product = ''
            for product in catalog.products:
#                Product += ('\n' + "Tên SP: " + product.name + '\n' + product.oldPrice + '\n' + product.salePrice + '\n' + product.quantity + '\n' + product.unit + '\n' + product.saleContent + '\n' + datetime.utcfromtimestamp(float(product.saleDateFrom)).strftime('%d/%m/%Y %H:%M:%S') + '\n' + datetime.utcfromtimestamp(float(product.saleDateTo)).strftime('%d/%m/%Y %H:%M:%S'))
                Product += ('\n' + "Tên sản phẩm: " + product.name + '\n' + "Giá cũ: " + product.oldPrice + '\n' + "Giá KM: " + product.salePrice + '\n' + "S.Lượng: " + product.quantity + '\n' + "Đơn vị: " + product.unit + '\n' + "Nội dung KM: " + product.saleContent + '\n' + "Thời gian áp dụng: " + datetime.utcfromtimestamp(float(product.saleDateFrom)).strftime('%d/%m/%Y %H:%M:%S') + '\n' + "Thời gian kết thúc: " + datetime.utcfromtimestamp(float(product.saleDateTo)).strftime('%d/%m/%Y %H:%M:%S'))
                Sum_product_lv2 += len(product.name) + len(product.oldPrice) + len(product.salePrice) + len(product.quantity) + len(product.unit) + len(product.saleContent) + len(datetime.utcfromtimestamp(float(product.saleDateFrom)).strftime('%d/%m/%Y %H:%M:%S')) + len(datetime.utcfromtimestamp(float(product.saleDateTo)).strftime('%d/%m/%Y %H:%M:%S')) + 8 + 99


            WriteProduct += Product
            Sum_product_lv1 +=Sum_product_lv2

        MF = 0
        if (((Sum_store + Sum_product_lv1)% 15) != 0):
            MF = (Sum_store + Sum_product_lv1)// 15
        else:
            MF = ((Sum_store + Sum_product_lv1)// 15)
        with open('catalogue.txt','w+') as a:
            a.write("{" + store.id + "#" + str(Sum_store + Sum_product_lv1) + "#" + "0" + "#" + str(MF) + "}")
            a.write(WriteStore)
            a.write(WriteProduct)
            a.write('\n')
        a.close()

    def deviceStatus(store):
        connection = pika.BlockingConnection(
            pika.ConnectionParameters(host='funnybunny-ui.7perldata.win'))
        channel = connection.channel()

        channel.queue_declare(queue='device_status')

        channel.basic_publish(exchange='', routing_key='device_status', body= 'c3dca840-14b3-46af-856a-a2fb32a865be')
        print("Sent  status Advertiser!")
        connection.close()

    #########################################################
    main()
    jsonObj = json.loads(message)
    store = parseData(jsonObj)
    printData(store)
    deviceStatus(store)
#    main()
#-------------------------------
threadClient()

try:
    func_threadClient = threading.Thread(target = threadClient)
#    thread_printData = threading.Thread(target = printData(store))
except Exception as e:
    print("Erro threadClient...")

while True:
    if not (func_threadClient.isAlive()):
       func_threadClient_new = threading.Thread(target = threadClient)
#        thread_printData_new = threading.Thread(target = printData(store))
       func_threadClient_new.start()
#       thread_printData_new.start()
       time.sleep(5)
