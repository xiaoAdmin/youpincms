package youpincms

import com.taobao.api.domain.TaobaokeItem
import com.youpin.cms.Goods
import com.youpin.cms.util.TaobaoApi

class GoodsFinderService {
	/**
	 * 开始
	 */
	def start() {
		println 'catch'+ new Date()
		/*
		 * 1.查询商品
		 * 2.找出新商品,存入数据库
		 * 3.旧商品更新数据
		 */
		//TODO:添加商品分类参数
		['米折网专享','天天特价'].each {keyword->
			[16,21,30,33,50008075,50002766].each {cid->
				(1..5).each {pageNo->
					processGoods(keyword,cid,pageNo, 100)
				}
			}
		}
	}

	def processGoods(String keyword,long cid,long pageNo,long pageSize){
		def remoteGoods = findGoods(keyword,cid,pageNo, pageSize)
		def localGoodsMap = loadLocalGoods()
		remoteGoods.each {
			if (localGoodsMap.keySet().contains(it.numId)) {
				Goods goods = localGoodsMap.get(it.numId)
				[
					'originalPrice',
					'originalPriceDes',
					'promotionPrice',
					'promotionPriceDes',
					'updateDate',
					'updateDateDes'
				].each {property->
					goods[property] = it[property]
				}
				goods.save(flush: true)
			}else{
				it.cid = cid
				it.save()
			}
		}
		return remoteGoods.size()
	}

	def Map loadLocalGoods(){
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, -7);
		def timestamp = c.getTime();

		def query = Goods.where { (creationDate > timestamp) }
		def map=[:]
		query.list()?.each{
			map[it.numId]=it
		}
		map
	}

	/**
	 * 获取goods列表
	 * @return
	 */
	def List<Goods> findGoods(String keyword,long cid,long pageNo,long pageSize){
		def tao = new TaobaoApi()
		def taokeList = tao.searchTaoke(keyword,cid, pageNo, pageSize, "desc")
		taokeList.collect { createGoods(it) }
	}

	/**
	 * 根据taokeitem创建goods
	 * @param item
	 * @return
	 */
	def Goods createGoods(TaobaokeItem item){
		def goods = new Goods([
			numId:item.numIid,
			nick:item.nick,
			goodsName:item.title,
			originalPrice:Double.parseDouble(item.price),
			originalPriceDes:item.price,
			goodsLocation:item.itemLocation,
			sellerCreditScore:item.sellerCreditScore,
			clickUrl:item.clickUrl,
			shopClickUrl:item.shopClickUrl,
			picUrl:item.picUrl,
			commissionRate:item.commissionRate,
			commission:item.commission,
			commissionNum:item.commissionNum,
			commissionVolume:item.commissionVolume,
			volume:item.volume,
			promotionPrice:Double.parseDouble(item.promotionPrice),
			promotionPriceDes:item.promotionPrice
		])
		//TODO:找个好点的正则
		goods['goodsName'] = goods.getGoodsName().
				replaceAll("^((【)?米折网专享(】)?)|(<[a-zA-Z]+[1-9]?[^><]*>)|(</[a-zA-Z]+[1-9]?>)", "")
		return goods
	}

}

