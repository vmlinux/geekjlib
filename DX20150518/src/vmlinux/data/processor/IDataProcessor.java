package vmlinux.data.processor;

import vmlinux.data.entity.OData;

public interface IDataProcessor
{
	public void process(OData conf) throws Exception;
}
